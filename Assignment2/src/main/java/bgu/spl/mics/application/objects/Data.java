package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;

    Data(Type type_, int size_){
        type=type_;
        size=size_;
        processed = 0;
    }

    public Type getType(){
        return type;
    }

    public int getSize(){return size;}

    public void addProcessedData(){
        processed += 1000;
    }

    public  int getProcessed(){return processed;}

    public String toString(){
        return "Type: "+type+'\n'+"Size: "+size+'\n'+"Processed: " + processed;
    }


}
