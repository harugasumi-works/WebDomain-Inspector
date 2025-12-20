public enum LogLevel {
INFO(1),
WARNING(2),
CRITICAL(3);

    private final int value;

    private LogLevel (int value){
        this.value = value;
    }

    public int getValue (){
        return value;
    }
}
