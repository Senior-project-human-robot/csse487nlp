package Models;

import java.util.ArrayList;

public class TargetModel extends ItemModel {
    private RelationModel relationModel;

    public TargetModel(){
        super();
        this.relationModel = new RelationModel();
    }

    public TargetModel(ItemModel itemModel, RelationModel relationModel){
        super(itemModel.getItem(), itemModel.getMods(), itemModel.getGesture(), itemModel.getBelonging());
        this.relationModel = relationModel;
    }

    public TargetModel(String item,
                       ArrayList<String> mods,
                       Boolean gesture,
                       String belonging,
                       RelationModel relationModel){
        super(item, mods, gesture, belonging);
        this.relationModel = relationModel;
    }

    public RelationModel getRelationModel() {
        return relationModel;
    }

    public void setRelationModel(RelationModel relationModel) {
        this.relationModel = relationModel;
    }
}
