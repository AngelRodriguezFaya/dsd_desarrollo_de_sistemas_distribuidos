import os

#Agregar el directorio gen-py al path de python
sys.path.append(os.path.join(os.path.dirname(file), 'gen-py'))

# Con esto, podemos poner el cliente y el servidor al mismo nivel que el archivo thrift.
# La idea es hacerlo multiplataforma, pues esa es la idea por la que nació Apache Thrift
# el Servidor (en Java) y el Cliente (en Python) 