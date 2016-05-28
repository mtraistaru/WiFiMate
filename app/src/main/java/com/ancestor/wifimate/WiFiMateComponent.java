package com.ancestor.wifimate;

import com.ancestor.wifimate.activity.ChatActivity;
import com.ancestor.wifimate.activity.MainActivity;
import com.ancestor.wifimate.fragment.PeerDetailsFragment;
import com.ancestor.wifimate.module.WiFiMateModule;
import com.ancestor.wifimate.network.StreamSenderTCP;
import com.ancestor.wifimate.peer.Receiver;
import com.ancestor.wifimate.peer.Sender;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Mihai.Traistaru on 22-Apr-16.
 */
@Singleton
@Component(modules = {WiFiMateModule.class})
public interface WiFiMateComponent {

    void inject(MainActivity mainActivity);

    void inject(ChatActivity chatActivity);

    void inject(PeerDetailsFragment peerDetailsFragment);

    void inject(StreamSenderTCP streamSenderTCP);

    void inject(Receiver receiver);

    void inject(Sender sender);
}
