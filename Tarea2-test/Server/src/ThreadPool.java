package Server;

public class ThreadPool {
    private int Max;
    private int N_thread;

    public ThreadPool (int Max) {
        this.Max = Max;
        this.N_thread = 0;
    }

    public void quitar_thread() {
        this.N_thread --;
    }

    public Boolean anadir_thread () {
        if(Max > N_thread) {
            this.N_thread ++;
            return Boolean.TRUE;
        }else {
            return Boolean.FALSE;
        }
    }
}
