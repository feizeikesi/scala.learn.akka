import akka.actor.*;
import scala.Option;
import scala.PartialFunction;

import scala.concurrent.Promise;
import scala.runtime.BoxedUnit;

import java.util.concurrent.Future;


/**
 * Created by Lei on 2017-4-25.
 */
public class BasicActor extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Throwable {

    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
    }

}


