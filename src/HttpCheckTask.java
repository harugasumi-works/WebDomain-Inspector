import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;


public class HttpCheckTask implements Task{
    private final LogLevel level;
    private final String url;
    private Consumer<String> logger;
    /**
     * HttpClient　インスタンス
     */
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL) 
            .build();

    
    /** 
     * ロガーを設定します。
     * @param logger ログメッセージを受け取るためのコンシューマーを設定します。
     */
    @Override
    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }        

    /** 
     * ログメッセージを出力します。
     * @param message
     */
    private void log(String message) {
    if (logger != null) {
        logger.accept(message);
    }
}
    /**
     * HttpCheckTaskのコンストラクタです。
     * @param level LogLevelからの優先度
     * @param url links.txtにあるURL文字列
     */
    public HttpCheckTask(LogLevel level, String url) {
        this.level = level;
        this.url = url;
    }

    /** 
     * 
     * @return Boolean
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        try {
            /**
             * <p> このコードはJavaの{code: Builder}パターンを利用して、{code: this.url}に向けた
             * 不変（Immutable）なHttpRequestオブジェクトを構築しています。
             * サーバーの応答が遅い場合にアプリがフリーズしないよう
             * 3秒のタイムアウトを設定し、サーバーからのアクセス拒否を回避するために「{code: User-Agent}」ヘッダーで
             * 自身を「Sentinel/1.0」（ブラウザを模倣）として識別させています。最も重要な点は、通常の「GET」
             * ではなく<b>「HEAD」</b>メソッドを指定していることです。これにより、サーバーはコンテンツの中身
             * （ボディ）を送らずにヘッダー情報のみを返すよう指示されるため、
             * ページ全体をダウンロードすることなくURLの生存確認（Pingのような動作）を
             * 非常に少ない通信量（帯域幅）で行うことができます。</p>
             */
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url))
                    .timeout(Duration.ofSeconds(3)) 
                    .header("User-Agent", "Mozilla/5.0 (Sentinel/1.0)")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()) 
                    .build();

            /**
             * この行は、事前に設定された {code: HttpClient} を使用してネットワークリクエストを 同期的 に実行し、
             * サーバーからの応答があるまでプログラムの実行を一時停止させます。
             * 先ほど作成した {code: request} オブジェクトを送信すると同時に、
             * {code: HttpResponse.BodyHandlers.discarding()} というボディハンドラを指定しています。
             * これは、サーバーから送られてくるボディデータ（コンテンツ）をメモリに保存せず、
             * 即座に破棄（無視）するようクライアントに指示するものです。その結果、
             * 返される {code: HttpResponse<Void>} オブジェクト（変数 {code: response}）には、
             * ステータスコード（200や404など）のようなメタデータのみが含まれ、
             * コンテンツデータは一切含まれないため、この確認処理においてメモリ効率が最大化されます。
             */
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            String msg = String.format("[%d] %s", response.statusCode(), url);
           
            if (logger != null) {
             /**
             * @see private void log(String message)
             */
            log(msg);
            }
            return true;
           

        } catch (Exception e) {
           System.out.printf("[ERROR] %s - %s%n", url, e.getMessage());
           return false;
        }
    }



    /** 
     * @return int
     */
    public int getPriority() {
        return level.getValue();
    }
}

