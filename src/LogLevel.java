/**
 * この列挙形はURLの優先度を定めます。
 */

public enum LogLevel {

/**レベル１ */
INFO(1),
/**レベル２ */
WARNING(2),
/**レベル3 */
CRITICAL(3);

/**
 * 優先度の値
 */
    private final int value;
/**
 * 列挙形のコンストラクタです。
 */
    private LogLevel (int value){
        this.value = value;
    }
/**
 * １から３のそれぞれの優先番号を返します。 
 * @return LogLevel's value (1 to 3)
 */
    public int getValue (){
        return value;
    }
}
