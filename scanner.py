import multiprocessing

scanner = None
queue = multiprocessing.Queue()

CONSUMER_KEY = 'XnNKSZ2LHPmb7yol1XuyHQ'
CONSUMER_SECRET = 'bXE50Qk92QsaT3YiuE61ZT5GXcATgP56nMHMpcY71w'

def launch():
  global scanner
  if scanner is None:
    scanner = multiprocessing.Process(target=scan, args=(queue,))
    scanner.start()

def add(desc):
  queue.put(dict(update=desc))
  launch()

def remove(regid):
  queue.put(dict(remove=regid))
  launch()

def scan(q):
  import Queue
  import gevent
  import gevent.monkey
  gevent.monkey.patch_all()

  tasks = dict()
  def listen_target(regid, token_compound):
    from twitter import TwitterStream, OAuth, TwitterHTTPError
    while True:
      token, token_secret = token_compound.split(u':')
      print 'listening %r for %s' % (token, regid)
      try:
        t = TwitterStream(auth=OAuth(token=token, token_secret=token_secret, consumer_key=CONSUMER_KEY, consumer_secret=CONSUMER_SECRET), domain='userstream.twitter.com')
        for tw in t.user():
          print tw
      except TwitterHTTPError, e:
        print 'encountered error, sleeping 15 sec and retrying: %s' % e
        gevent.sleep(15)
    
  def replace_target(regid, tokens):
      task = tasks.get(regid, [])
      for t in task:
          t.kill()
      if tokens:
        tasks[regid] = [gevent.spawn(listen_target, regid, token) for token in tokens]
      else:
        try:
          del tasks[regid]
        except KeyError:
          pass
    
  def update_target():
    try:
      while True:
        d = q.get_nowait()
        if 'remove' in d:
          regid = d['remove']
          replace_target(regid, None)
        if 'update' in d:
          desc = d['update']
          replace_target(desc['regid'], desc['tokens'])
    except Queue.Empty:
      pass
    return tasks
  def run():
    update_target()
    while not gevent.wait(timeout=0.1):
      update_target()
    
  while True:
    run()
    gevent.sleep(0.1)
