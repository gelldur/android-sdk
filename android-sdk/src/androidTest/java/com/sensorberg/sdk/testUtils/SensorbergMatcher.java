package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static org.mockito.Matchers.argThat;

public class SensorbergMatcher {

    public static ScanEvent isEntryEvent() {
        return argThat(new IsKindOfEntryEvent(true));
    }

    public static ScanEvent isExitEvent() {
        return argThat(new IsKindOfEntryEvent(false));
    }

    public static BeaconEvent hasDelay(int delayInMillies) {
        return argThat(new HasDelay(delayInMillies));
    }

    public static ScanEvent hasBeaconId(BeaconId otherBeaconId) {
        return argThat(new HasBeaconId(otherBeaconId));
    }

    private static class IsKindOfEntryEvent extends BaseMatcher<ScanEvent> {

        private final boolean isEntry;
        private ScanEvent actual;

        public IsKindOfEntryEvent(boolean isEntry) {
            this.isEntry = isEntry;
        }

        @Override
        public boolean matches(Object o) {
            actual = (ScanEvent) o;
            return (actual.isEntry() == isEntry);
        }

        @Override
        public void describeTo(Description description) {
            if (actual == null) {
                description.appendText("There was no event to evaluate");
            } else {
                if (isEntry){
                    description.appendText("It was an Exit Event");
                } else {
                    description.appendText("It was an Enter Event");
                }
            }
        }
    }

    private static class HasBeaconId extends  BaseMatcher<ScanEvent> {

        private BeaconId beaconId;
        private BeaconId actual;

        public HasBeaconId(BeaconId beaconId) {
            this.beaconId = beaconId;
        }

        @Override
        public boolean matches(Object o) {
            actual = ((ScanEvent) o).getBeaconId();
            return ( beaconId.equals(new BeaconId(actual.getBeaconId())));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("beacon ID \"%s\" did not match with \"%s\"", beaconId.toTraditionalString(), actual.toTraditionalString()));
        }
    }

    private static class HasDelay extends BaseMatcher<BeaconEvent> {
        private final int delayInMillies;
        private long actual;

        public HasDelay(int delayInMillies) {
            this.delayInMillies = delayInMillies;
        }

        @Override
        public boolean matches(Object o) {
            BeaconEvent incoming = (BeaconEvent) o;
            actual = incoming.getAction().getDelayTime();
            return actual == delayInMillies;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("delaytime was not " + delayInMillies + " but was instead " + actual);
        }
    }
}
