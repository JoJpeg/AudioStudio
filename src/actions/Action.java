package actions;
 

public interface Action {
    public Action execute();
    public Action undo();
    public Object getData();
}
