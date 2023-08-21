#!/bin/bash

# 自己署名ルートCA作成
openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout serverCA.key -out serverCA.crt
# パスフレーズはchangeit
# CNはmtls.sample.shibadog.netにしてみた。

# サーバ側の証明書作成
openssl req -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr
# パスフレーズはchangeit
# CNはlocalhost

# ルートCA証明書（rootCA.crt）とその 秘密鍵（rootCA.key）を使用してリクエストに署名
openssl x509 -req -CA serverCA.crt -CAkey serverCA.key -in localhost.csr -out localhost.crt -days 365 -CAcreateserial -extfile localhost.ext

# キーストアファイルにインポートするための p12 形式を作成する
openssl pkcs12 -export -out localhost.p12 -name "localhost" -inkey localhost.key -in localhost.crt

# keystoreを作成する
keytool -importkeystore -srckeystore localhost.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS

# 自己署名ルートCA作成
openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout clientCA.key -out clientCA.crt

# クライアント側を作る
openssl req -new -newkey rsa:4096 -nodes -keyout client.key -out client.csr

# CAで証明書に署名をする
openssl x509 -req -CA clientCA.crt -CAkey clientCA.key -in client.csr -out client.crt -days 365 -CAcreateserial

# クライアントも p12 を作る
openssl pkcs12 -export -out client.p12 -name "client" -inkey client.key -in client.crt

# トラストストアを作成する
# パスワードはchangeit
keytool -importcert -noprompt -alias ca -file clientCA.crt -keystore server-truststore.p12
