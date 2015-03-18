package guice;


import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.contrib.pattern.ClusterSingletonManager;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import worker.Master;


public class AkkaModule extends AbstractModule {

    public static final String ROLE = "test_role";
    public static final int PORT = 2551;




    @Override
    protected void configure() {
        //bind and start the subsystem
        bind(EntityIndex.class).to( EntityIndexImpl.class ).asEagerSingleton();
    }


    /**
     * Provides the singleton root-actor-system to be injected whenever an ActorSystem is required.  This method also
     * registers the GuiceAkkaExtension to be used for instantiating guice injected actors.
     */
    @Provides
    @Singleton
    public ActorSystem provideActorSystem(final Config conf) {
        ActorSystem system = ActorSystem.create( "ClusterSystem", conf );

        return system;

        //       startupSharedJournal( system, ( port == 2551 ),
        //               ActorPath$.MODULE$.fromString( "akka.tcp://ClusterSystem@127.0.0.1:2551/user/store" ) );



    }

    @Provides
    @Singleton
    public Config provideConfig(){
        final Config conf = ConfigFactory.parseString( "akka.cluster.roles=[" + ROLE + "]" ).
                       withFallback( ConfigFactory.parseString( "akka.remote.netty.tcp.port=" + PORT ) ).
                                                  withFallback( ConfigFactory.load() );

        return conf;
    }
}
