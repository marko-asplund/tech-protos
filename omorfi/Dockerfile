
FROM ubuntu:16.04

RUN apt-get update && apt-get install -y locales \
    && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

RUN apt-get install -y sudo less vim git wget man python3 zip xz-utils gcc software-properties-common

RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 seen true" | debconf-set-selections
RUN apt-get install -y oracle-java8-installer

RUN /bin/bash -c "wget https://apertium.projectjj.com/apt/install-release.sh -O - | sudo bash"
RUN apt-get install -y apertium-dev hfst hfst-ospell python3-libhfst libhfst-dev pkg-config time

RUN sed -i'' 's/\(%sudo.*\)ALL$/\1 NOPASSWD: ALL/' /etc/sudoers

RUN useradd -m omor -G sudo
RUN mkdir /omorfi
RUN chown omor:omor /omorfi

USER omor
WORKDIR /omorfi
RUN wget https://github.com/marko-asplund/hfst-optimized-lookup/releases/download/79e1fd7/hfst-ol.79e1fd7.jar
ENV CLASSPATH /omorfi/hfst-ol.79e1fd7.jar
RUN wget https://github.com/flammie/omorfi/releases/download/20170515/omorfi-20170515.tar.xz
RUN tar xvfJ omorfi-20170515.tar.xz
WORKDIR omorfi-20170515
RUN ./configure
RUN ./configure --enable-labeled-segments --enable-lemmatiser --enable-segmenter
RUN make
RUN sudo make install
