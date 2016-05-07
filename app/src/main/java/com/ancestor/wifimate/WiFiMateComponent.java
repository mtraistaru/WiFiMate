package com.ancestor.wifimate;

import com.ancestor.wifimate.activity.MainActivity;
import com.ancestor.wifimate.activity.MessageActivity;
import com.ancestor.wifimate.fragment.DeviceDetailFragment;
import com.ancestor.wifimate.module.WiFiMateModule;
import com.ancestor.wifimate.network.StreamSenderTCP;
import com.ancestor.wifimate.peer.Receiver;
import com.ancestor.wifimate.peer.Sender;
import com.ancestor.wifimate.receiver.WiFiBroadcastReceiver;
import com.ancestor.wifimate.receiver.WiFiDirectBroadcastReceiver;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Mihai.Traistaru on 22-Apr-16.
 */
@Singleton
@Component(modules = {WiFiMateModule.class})
public interface WiFiMateComponent {

    void inject(MainActivity mainActivity);

    void inject(MessageActivity messageActivity);

    void inject(DeviceDetailFragment deviceDetailFragment);

    void inject(StreamSenderTCP streamSenderTCP);

    void inject(Receiver receiver);

    void inject(WiFiBroadcastReceiver wiFiBroadcastReceiver);

    void inject(WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver);

    void inject(Sender sender);
}
