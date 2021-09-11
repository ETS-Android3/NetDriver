package lb.com.thenet.netdriver.ui.blockui;

import lb.com.thenet.netdriver.rooms.entities.stops.Stop;

public interface OnBlockFragmentInteraction {
        void viewStop(Stop stop);
        void rejectStop(Stop stop);
}
