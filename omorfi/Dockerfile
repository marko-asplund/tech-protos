
FROM ubuntu:16.04

RUN apt-get clean && apt-get update

RUN apt-get install -y sudo less git wget man python3 zip xz-utils gcc software-properties-common locales

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 seen true" | debconf-set-selections
RUN apt-get install -y oracle-java8-installer

RUN /bin/bash -c "wget https://apertium.projectjj.com/apt/install-release.sh -O - | sudo bash"
RUN apt-get install -y apertium-dev hfst hfst-ospell python3-libhfst libhfst-dev pkg-config time

RUN mkdir /omorfi
WORKDIR /omorfi
RUN wget https://github.com/flammie/omorfi/releases/download/20170515/omorfi-20170515.tar.xz
RUN tar xvfJ omorfi-20170515.tar.xz
WORKDIR omorfi-20170515

#RUN useradd -m hfst -G sudo
#USER hfst
#WORKDIR /home/hfst
