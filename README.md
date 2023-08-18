# mTLS実装テスト

## overview

mTLSをSpring Bootで実装した場合どのような設定・実装が必要なのか試すために作成した。

以下のような構成となる。

* mtls-server: リクエストを受ける側。こちらはSSLでリクエストを受ける。
* mtls-client: リクエストを投げる側。こちらはhttpリクエストを受けたのち、mtls-serverへHTTPSでリクエストを行う。
* mtls-test: karateを使ってリクエストを試すためのプロジェクト

各アプリケーションには以下のようにキーを配布済み。

![key](./doc/certs_image.drawio.svg)

## 参考資料

基本的に、ソースや手順は以下のサイトを参考に作成。  
※ Spring Bootのバージョン差異やJDKのバージョン差異による修正が発生しているため、以下の記事とはソースコードが一部異なります。

[[Spring boot] mTLS相互認証の実装サンプル - プログラミング初心者ナビ！](https://4engineer.net/)
