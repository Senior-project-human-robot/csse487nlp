package Models;

import utils.Utils;

import java.util.ArrayList;

public class ItemModel {
    private String item;
    private ArrayList<String> mods;
    private Boolean gesture;
    private String belonging;

    public ItemModel(){
        item = Utils.NOT_FOUND;
        belonging = Utils.NOT_FOUND;
        mods = new ArrayList<>();
        gesture = false;
    }

    public ItemModel(String item,
                     ArrayList<String> mods,
                     Boolean gesture,
                     String belonging){
        this.item = item;
        this.mods = mods;
        this.gesture = gesture;
        this.belonging = belonging;
    }


    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public ArrayList<String> getMods() {
        return mods;
    }

    public void setMods(ArrayList<String> mods) {
        this.mods = mods;
    }

    public Boolean getGesture() {
        return gesture;
    }

    public void setGesture(Boolean gesture) {
        this.gesture = gesture;
    }

    public String getBelonging() {
        return belonging;
    }

    public void setBelonging(String belonging) {
        this.belonging = belonging;
    }
}
