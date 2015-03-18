package guice;


import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.contrib.pattern.ClusterClient;
import akka.contrib.pattern.ClusterSingletonManager;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import worker.Frontend;
import worker.Master;
import worker.WorkExecutor;
import worker.WorkProducer;
import worker.WorkResultConsumer;
import worker.Worker;


@Singleton
public class EntityIndexImpl implements EntityIndex {

    //give up on anything that takes longer than 10 seconds to process
    private static FiniteDuration workTimeout = Duration.create( 10, "seconds" );

    private final ActorSystem actorSystem;
    private final Config conf;


    @Inject
    public EntityIndexImpl( final ActorSystem actorSystem, final Config conf ) {
        this.actorSystem = actorSystem;
        this.conf = conf;

        startMaster();
        startWorkers();
        startFrontEnd();
    }


    public void startMaster() {
        //wire upt the root actor
        actorSystem.actorOf( ClusterSingletonManager
                        .defaultProps( Master.props( workTimeout ), "active", PoisonPill.getInstance(),
                                AkkaModule.ROLE ), "master" );
    }


    public void startWorkers() {


        Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();

        for ( String contactAddress : conf.getStringList( "contact-points" ) ) {
            initialContacts.add( actorSystem.actorSelection( contactAddress + "/user/receptionist" ) );
        }

        final ActorRef clusterClient =
                actorSystem.actorOf( ClusterClient.defaultProps( initialContacts ), "clusterClient" );
        actorSystem.actorOf( Worker.props( clusterClient, Props.create( WorkExecutor.class ) ), "worker" );
    }


    public void startFrontEnd(){

       ActorRef frontend = actorSystem.actorOf(Props.create( Frontend.class), "frontend");
        actorSystem.actorOf(Props.create( WorkProducer.class, frontend), "producer");
        actorSystem.actorOf(Props.create(WorkResultConsumer.class), "consumer");
    }
}
