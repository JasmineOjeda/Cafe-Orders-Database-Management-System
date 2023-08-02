CREATE INDEX timestamp_index
ON orders
(timeStampRecieved);

CREATE INDEX orderid_index
ON ItemStatus
( orderid );

CREATE INDEX login_index
ON Users
(login);

