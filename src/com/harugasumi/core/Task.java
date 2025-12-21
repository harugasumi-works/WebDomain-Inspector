package com.harugasumi.core;
import java.util.concurrent.Callable;

/**
 * @author harugasumi-works
 * このインターフェースは、タスクの基本的な構造を定義します。
 * 各タスクは優先度を持ち、ログ出力のためのロガーを設定できる必要があります。
 * {code: Callable<Boolean>} を拡張しており、タスクの実行結果を示すブール値を返します。
 */

public interface Task extends Callable<Boolean> { 
      /** 
      * @return タスクの優先度を整数で返します。
      */
   int getPriority();
      /** 
      * {code: Consumer<String>} 変数logger を受け取り、タスクのログ出力に使用します。
      * @param logger ログメッセージを受け取るためのコンシューマーを設定します。
      */
   void setLogger(java.util.function.Consumer<String> logger);
}