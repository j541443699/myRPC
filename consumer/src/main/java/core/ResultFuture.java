package core;

import entity.ClientRequest;
import entity.ServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jyq
 * @createTime 2023/10/4 22:29
 */
@Slf4j
public class ResultFuture {
    
    public final static Map<Long, ResultFuture> map = new ConcurrentHashMap<>();
    public final Lock lock = new ReentrantLock();
    public Condition condition = lock.newCondition();
    public ServerResponse response;
    
    public ResultFuture(ClientRequest request) {
        map.put(request.getRequestId(), this);
    }
    
    public ServerResponse get() {
        lock.lock();
        try {
            while (!done()) {
                condition.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            log.info(Thread.currentThread().getName() + " get处释放锁");
        }
        return response;
    }
    
    public static void receive(ServerResponse response) {
        if (response != null) {
            ResultFuture future = map.get(response.getRequestId());
            if (future != null) {
                Lock lock = future.lock;
                lock.lock();
                try {
                    future.setResponse(response);
                    future.condition.signal();
                    map.remove(response.getRequestId());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private boolean done() {
        if (response != null) {
            return true;
        }
        return false;
    }

    public void setResponse(ServerResponse response) {
        this.response = response;
    }
    
}
