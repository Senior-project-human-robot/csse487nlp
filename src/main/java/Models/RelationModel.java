package Models;

import utils.Utils;

import java.util.ArrayList;

public class RelationModel {
    private String Direction;
    private ArrayList<ItemModel> Objects;

    public RelationModel(String direction, ArrayList<ItemModel> objects) {
        this.Direction = direction;
        this.Objects = objects;
    }
    public RelationModel() {
        this.Direction = Utils.NOT_FOUND;
        this.Direction = Utils.NOT_FOUND;
    }
    public String getDirection(){
        return this.Direction;
    }

    public ArrayList<ItemModel> getObjects(){
        return this.Objects;
    }

    public void setDirection(String direction){
        this.Direction = direction;
    }

    public void setObjects(ArrayList<ItemModel> objects){
        this.Objects = objects;
    }
}

