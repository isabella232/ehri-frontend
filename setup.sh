#!/bin/bash
cat <<EOF
# This is a in-testing setup script built from the README instructions. 
# Run this at your own risk, only tested on Debian 9 and does not complete the process
# This script is not clean yet and does not check if things are already running. 
EOF

confirm() {
    # call with a prompt string or use a default
    read -r -p "${1:-Are you sure? [y/N]} " response
    case "$response" in
        [yY][eE][sS]|[yY]) 
            true
            ;;
        *)
            false
            ;;
    esac
}

if ! confirm; then
    exit 0
fi


# This app has a few depependencies in addition to the backend:

#  A PostgreSQL 9.5+ database
# Solr, running configurated as per the config in [EHRI Search Tools](https://github.com/EHRI/ehri-search-tools)
# Instead of installing these yourself we have a Docker container

install_docker(){
    # First we try detect the archtecture to add the right repo
    # Probably not necessary but its nice to do
    CURRENTUSER=$USER
    UNAMEMACHINE=$(uname -m) 
    case $UNAMEMACHINE in 
        aarch64)
            arch="arm64"
            ;;
        amd64)
            arch="amd64"
            ;;
        *)
            arch="amd64"
    esac
    sudo apt update
    # Grab some needed stuff from apt
    sudo apt install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg2 \
        software-properties-common
    # Add the docker siging key
    curl -fsSL https://download.docker.com/linux/debian/gpg \
        | sudo apt-key add -
    # Add the repository 
    sudo add-apt-repository \
           "deb [arch=$arch] https://download.docker.com/linux/debian \
              $(lsb_release -cs) \
                 stable"
    # Update & Install 
    sudo apt update 
    sudo apt install -y \
        docker-ce 
    # Add to the packages file to stop the script installing more in
    # other scripts 
    # Add user to docker group
    sudo usermod -a -G $CURRENTUSER
}

# If Docker isn't installed, install it 
if [[ ! $(command -v docker ) ]]; then
	install_docker
fi

# Set up the search engine on port 8983: 
sudo docker run --publish 8983:8983 -d -t ehri/ehri-search-tools
      
# Set up the backend web service on port 7474: 
sudo docker run --publish 7474:7474 -d -t ehri/ehri-rest
     
# Set up PostgreSQL (Dockerised) with the right schema: 
sudo docker run  -d -t -e POSTGRES_USER=docview -e POSTGRES_PASSWORD=changeme --publish 5432:5432 postgres

# Create an additional group on the backend named "portal":

curl  --header content-type:application/json \
      --header X-User:admin \
      --data-binary '{
           "id":"portal", 
           "type":"Group",
           "data":{"identifier": "portal", "name":"Portal"}
      }' \
      http://localhost:7474/ehri/classes/Group

# Install postfix or a suitable email-sending program
if [[ ! $(command -v postfix ) ]]; then
	sudo apt install -y postfix
fi

# Install Node JS (which handles client-side asset compilation)
if [[ ! $(command -v npm) ]]; then
    curl -sL https://deb.nodesource.com/setup_11.x | sudo -E bash -
    sudo apt install -y nodejs
fi

if [[ ! $(command -v java) ]]; then
    sudo apt install default-jdk default-jre
fi

# Install [sbt](http://www.scala-sbt.org/release/docs/Setup.html)
if [[ ! $(command -v sbt) ]]; then
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    if [[ ! $(command -v dirmngr) ]]; then
        sudo apt install -y dirmngr
    fi
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
    if [[ "$?" != "0" ]]; then
        TEMPKEY=$(mktemp)
        gpg --recieve-keys 2EE0EA64E40A89B84B2DF73499E82A75642AC823
        gpg --export 2EE0EA64E40A89B84B2DF73499E82A75642AC823 > $TEMPKEY 
        sudo apt-key add $TEMPKEY
        rm $TEMPKEY
    fi
    sudo apt update
    sudo apt install -y sbt
fi

sbt run

exit 0

# Go to localhost:9000
www-browser localhost:9000
cat <<EOF
    When you get a message about database evolutions being required, click "Apply this script now"
    Create an account at http://localhost:9000/login
    Get your new account ID, which can be found by looking at the URL for you account on the people page (`http://localhost:9000/people`). It should be `user000001`.
    Make developer account an admin on the backend (replace `{userID}` with actual ID):
EOF
userID="user000001"

exit 0
 
curl -X POST \
        --header X-User:admin \
        http://localhost:7474/ehri/classes/Group/admin/${userID}
 
cat <<EOF
make account verified and staff on the front end (replace {userId} with actual ID and use default password 'changeme'):
EOF
 
psql -hlocalhost -Udocview docview \
        -c "update users set verified = true, staff = true where id = '${userID}'"

cat <<EOF
At this point you should be able to access the admin pages and create data, e.g:

 - create a country record at `http://localhost:9000/admin/countries/create`. You only have to provide the country code, e.g. "us"
 - create an institution in that country
 - create archival records in the institution

NOTE: certain functionality also depends on a valid AWS S3 configuration set in the `conf/aws.conf` file.
Use the `conf/aws.conf.example` as a template.
EOF 

### Testing
# 
# Running integration tests requires an instance of the backend service running locally on port 7575. This can be done with a single Docker command:
# 
#     sudo docker run --publish 7575:7474 -it ehri/ehri-rest
