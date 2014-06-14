import dbm
import json

from flask import Flask
import scanner

app = Flask(__name__)

class DeviceDB(object):
  def __init__(self):
    self.db = dbm.open('devices', 'c')

  def __enter__(self):
    pass

  def __exit__(self):
    self.db.close()

@app.route('/<regid>', methods=['GET'])
def get_tokens(regid):
  db = dbm.open('devices', 'c')
  try:
    return db.get(regid, b'[]').decode()
  finally:
    db.close()

@app.route('/<regid>', methods=['PUT'])
def register_device(regid):
  db = dbm.open('devices', 'c')
  try:
    db[regid] = b'[]'
    return '{success:true}'
  finally:
    db.close()

@app.route('/<regid>', methods=['DELETE'])
def unregister_device(regid):
  db = dbm.open('devices', 'c')
  try:
    try:
      del db[regid]
      return '{success:true}'
    except KeyError:
      return '{error:\"not-found\"}'
  finally:
    db.close()

@app.route('/<regid>/<access_token>/<access_token_secret>', methods=['PUT'])
def register_token(regid, access_token, access_token_secret):
  db = dbm.open('devices', 'c')
  try:
    entry = set(json.loads(db.get(regid, b'[]').decode()))
    entry.add('%s:%s' % (access_token, access_token_secret))
    db[regid] = json.dumps(list(entry)).encode()
    return '{success:true}'
  finally:
    db.close()

@app.route('/<regid>/<access_token>/<access_token_secret>', methods=['DELETE'])
def unregister_token(regid, access_token, access_token_secret):
  db = dbm.open('devices', 'c')
  try:
    entry = set(json.loads(db.get(regid, b'[]').decode()))
    entry.discard('%s:%s' % (access_token, access_token_secret))
    db[regid] = json.dumps(list(entry)).encode()
    return '{success:true}'
  finally:
    db.close()

@app.route('/<regid>/watch', methods=['PUT'])
def start(regid):
  db = dbm.open('devices', 'c')
  try:
    scanner.add(dict(regid=regid, tokens=json.loads(db.get(regid, b'[]').decode())))
    return '{success:true}'
  finally:
    db.close()

@app.route('/<regid>/watch', methods=['DELETE'])
def stop(regid):
  db = dbm.open('devices', 'c')
  try:
    scanner.remove(regid)
    return '{success:true}'
  finally:
    db.close()


if __name__ == '__main__':
  app.debug = True
  app.run()
