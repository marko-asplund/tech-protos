
FROM ubuntu:18.04

RUN apt-get update && apt-get install -y locales \
    && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

RUN apt-get install -y sudo less vim git wget man-db python3 zip xz-utils gcc software-properties-common

RUN apt-get install -y openjdk-8-jdk-headless

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
RUN wget https://github.com/flammie/omorfi/releases/download/20190511/omorfi-20190511.tar.xz
RUN tar xvfJ omorfi-20190511.tar.xz
WORKDIR omorfi-20190511
RUN ./configure
RUN ./configure --enable-labeled-segments --enable-lemmatiser --enable-segmenter
RUN make
RUN sudo make install

RUN echo "export PYTHONPATH=/usr/local/lib/python3.5/site-packages/" >> ~/.bashrc
RUN echo "export LD_LIBRARY_PATH=/usr/local/lib" >> ~/.bashrc

# Enable locating files
RUN sudo apt-get install locate
RUN sudo /etc/cron.daily/locate

# Voikko
RUN sudo apt-get -y install python3-libvoikko

# Apertium
RUN sudo apt-get -y install apertium dh-autoreconf cg3

RUN sudo mkdir /apertium
RUN sudo chown omor:omor /apertium
WORKDIR /apertium

# Apertium, Finnish
RUN git clone https://github.com/apertium/apertium-fin.git apertium-fin
WORKDIR apertium-fin
RUN ./autogen.sh
RUN ./configure
RUN make
RUN sudo make install

# Apertium, Portuguese
RUN sudo apt-get -y install lttoolbox python
WORKDIR /apertium
RUN git clone https://github.com/apertium/apertium-por.git apertium-por
WORKDIR apertium-por
RUN ./autogen.sh
RUN ./configure
RUN make
RUN sudo make install

# Voikko development and dictionary
RUN sudo apt-get -y install voikko-fi libvoikko-dev

# Foma
RUN sudo apt-get -y install foma-bin

# Graphviz
RUN sudo apt-get -y install graphviz

# Portuguese examples
WORKDIR /apertium
RUN git clone https://github.com/dustedmtl/lang-tech.git lt
WORKDIR lt/port-foma
# RUN git pull
RUN ./build.sh
