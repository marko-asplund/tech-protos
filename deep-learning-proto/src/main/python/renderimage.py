import argparse
import matplotlib.pyplot as plt
import numpy as np
import struct
import h5py

# tool for rendering images in the dataset

ImgHeight = 64
ImgWidth = 64
ImgDepth = 3
ImgPixels = ImgHeight * ImgWidth * ImgDepth


def load_data(fileName):
    train_dataset = h5py.File(fileName, "r")
    train_set_x_orig = np.array(train_dataset["train_set_x"][:])

    return train_set_x_orig

def load_from_file(fileName):
    with open(fileName, 'rb') as f:
        data = f.read()
    return np.array(struct.unpack(str(ImgPixels) + "B", data)).astype('uint8').reshape((ImgHeight, ImgWidth, ImgDepth))

parser = argparse.ArgumentParser(description='Render image')
parser.add_argument('-i', required=True, type=int, help="image index")
args = parser.parse_args()

#d1 = load_from_file("image14.bin")
d = load_data("/Users/aspluma/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5")

plt.figure(1)
plt.imshow(d[args.i])
plt.show()
