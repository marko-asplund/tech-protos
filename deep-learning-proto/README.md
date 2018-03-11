
# Deep Neural Network for Image Classification

Implements a 2-layer and L-layer neural network based binary classifiers in Scala
using linear algebra libraries. 
There are two different implementations: One based on Breeze and another one with ND4J.

Training data is stored in HDF5 binary data format.
NetCDF library is used for loading the data.

# Viewing training set images

Images can be viewed with either `ImageRendered` or `renderimage.py`.


## Using Python image rendering tool

* install Virtualenv
* create new virtualenv and activate it
```
virtualenv dl-py
source dl-py/bin/activate
```
* install `matplotlib` and `h5py`
```
pip install matplotlib
pip install h5py
```

# TODO

