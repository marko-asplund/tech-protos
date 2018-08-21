
import numpy as np
import tensorflow as tf
from tensorflow import keras
from keras.models import Sequential
import h5py
from keras.utils.io_utils import HDF5Matrix

print("TensorFlow version: {}".format(tf.VERSION))

data_dir = '/Users/aspluma/projects/tech-protos/deep-learning-proto/datasets'
train_fn = data_dir + '/train_catvnoncat.h5'
test_fn = data_dir + '/test_catvnoncat.h5'
image_dims = (64, 64, 3)
train_epochs = 1000

X_train = HDF5Matrix(train_fn, 'train_set_x', normalizer = lambda x: x / 255.0)
y_train = HDF5Matrix(train_fn, 'train_set_y')
X_test = HDF5Matrix(test_fn, 'test_set_x', normalizer = lambda x: x / 255.0)
y_test = HDF5Matrix(test_fn, 'test_set_y')
m_train = X_train.shape[0]
m_test = X_test.shape[0]

#X_train.shape: (209, 64, 64, 3)
#y_train.shape: (209,)
model = tf.keras.Sequential([
  tf.keras.layers.Flatten(input_shape=image_dims),
  tf.keras.layers.Dense(20, activation="relu"),
  tf.keras.layers.Dense(7, activation="relu"),
  tf.keras.layers.Dense(5, activation="relu"),
  tf.keras.layers.Dense(1, activation="sigmoid")
])
from keras import optimizers

model.compile(loss=keras.losses.binary_crossentropy,
              optimizer=keras.optimizers.SGD(lr=0.01),
              metrics=['accuracy'])

model.fit(X_train, y_train, epochs=train_epochs, shuffle="batch", batch_size=m_train) # batch GD

loss_and_metrics = model.evaluate(X_test, y_test, batch_size=m_test)
print("loss and metrics: %s" % loss_and_metrics)

classes = model.predict(X_test)
labels_truth = np.array(y_test).reshape((1, m_test))
labels_pred = np.transpose(classes > 0.5).astype(int)
diff = np.equal(labels_truth, labels_pred).astype(int)
print(X_test.shape)
print(y_test.shape)
print("y_test: %s" % labels_truth)
print("preds : %s" % labels_pred)
print("diff  : %s" % diff)
print("predictions: %s" % np.transpose(classes))
print("correct: %s" % np.sum(diff))
print(np.unique(classes > 0.5, return_counts=True))
