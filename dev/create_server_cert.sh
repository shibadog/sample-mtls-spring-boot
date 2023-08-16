#!/bin/bash

# 自己署名ルートCA作成
openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout rootCA.key -out rootCA.crt
# パスフレーズはtest
# CNはlocalhostにしてみた。

# サーバ側の証明書作成
openssl req -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr
# パスフレーズはtest
# CNはlocalhost

# ルートCA証明書（rootCA.crt）とその 秘密鍵（rootCA.key）を使用してリクエストに署名
openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in localhost.csr -out localhost.crt -days 365 -CAcreateserial -extfile localhost.ext

