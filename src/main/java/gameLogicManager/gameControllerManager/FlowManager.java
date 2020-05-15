package gameLogicManager.gameControllerManager;

import gameLogicManager.gameModel.gameBoard.*;
import gameLogicManager.gameModel.player.*;

/**
 * This class is to control the game flow.
 * It is a facade class for controllers to communicate with entities and UI.
 * @author Rafi Coktalas
 * @version 10.05.2020
 */
public class FlowManager{

    private static FlowManager uniqueInstance; //Singleton

    // Controller Instances
    private ResourceController resourceController;
    private ActionController actionController;
    private AdjacencyController adjacencyController;

    private Player currentPlayer;
    private GameEngine gameEngine;
    private static Game game;

    public static FlowManager getInstance(){
        if( uniqueInstance == null ){
            uniqueInstance = new FlowManager();
        }
        return uniqueInstance;
    }

    private FlowManager(){
        resourceController = ResourceController.getInstance(); //TODO
        actionController = ActionController.getInstance(); //TODO
        adjacencyController = AdjacencyController.getInstance(); //TODO
        gameEngine = GameEngine.getInstance(); //TODO
    }

/*
    0: Action is successful.
    1: Not enough workers.
    2: Not enough power.
    3: Not enough coins.
    4: Not enough priests.
    5: Not enough victory points.
 */

    public void initializeGame(boolean isRandom) {
        game = Game.getInstance(isRandom); // + Players and their factions //TODO
    }

    /**
     * Terraform the given terrain if you have enough resources
     * @param terrainID	which terrain to tranform
     * @param newTerrainType chosen new type of the terrain
     * @return			whether terraform is successful or not
     */
    public int transformTerrain(int terrainID, TerrainType newTerrainType) {
        Terrain terrain = getTerrain(terrainID); // getTerrain returns Terrain object from the given id.

        /* Player cannot transform if the terrain is not available or it's the same terrain */
        if(!terrain.isAvailable() || terrain.getType().getTerrainTypeID() == newTerrainType.getTerrainTypeID()){
            return 4;
        }

        /* Check if the player has enough workers to have enough spades, obtain spades if possible */
        if(!resourceController.obtainSpade(currentPlayer, terrain.getType().getTerrainTypeID(), newTerrainType.getTerrainTypeID())){
            return 2;
        }

        actionController.transformTerrain(terrain, newTerrainType);

        //adjacencyController.updateAdjacencyList(currentPlayer, terrain);
        //Bu method score güncelleye bir method halini alacak

        return 0;
    }

    /**
     * Build dwelling on the given terrain if you have enough resources
     * @param terrainID	where the dwelling will be built on
     * @return			whether build is successful or not
     */
    public int buildDwelling(int terrainID)
    {
        Terrain terrain = getTerrain(terrainID);

        /* If the terrain is not empty(available), you cannot build a dwelling */
        if(!terrain.isAvailable()){
            return 4;
        }

        /* Chosen terrain must be adjacent to other structure terrains */
        if(!adjacencyController.isAdjacent(currentPlayer, terrain, game.getTerrainList())){
            return 5;
        }

        /* Check required resources and obtain resources if possible */
        if(resourceController.obtainResourceOfDwelling(currentPlayer) != 0){
            return resourceController.obtainResourceOfDwelling(currentPlayer);
        }

        actionController.build(currentPlayer, terrain);//create dwelling object on terrain, update attributes of player
        resourceController.obtainIncomeOfDwelling(currentPlayer);
        //adjacencyController.updateAdjacencyList(currentPlayer, terrain);

        return 0;
    }

    public int improveShipping()
    {
        /*shipping cannot be more than 3, cannot be upgraded anymore */
        if(currentPlayer.getShipping() == 3){
            return 6;
        }
        //Added a '!' since the obtainResourceForShipping returns true when player can afford coins & priests.
        if(resourceController.obtainResourceForShipping(currentPlayer) != 0){
            return resourceController.obtainResourceForShipping(currentPlayer);
        }

        actionController.improveShipping(currentPlayer);
        resourceController.obtainIncomeForShipping(currentPlayer);
        //adjacencyController.updateAdjacencyList(currentPlayer);

        return 0;
    }
    /**
     * Improve the terraforming skills
     * @return	0 if it is successful, otherwise a positive integer according to the reason of failure
     */
    public int improveTerraforming()
    {
        /*worker per spade cannot be less than 1, cannot be upgraded anymore */
        if(currentPlayer.getSpadeRate() == 1){
            return 6;
        }
        if(resourceController.obtainResourceForImprovement(currentPlayer) != 0){
            return resourceController.obtainResourceForImprovement(currentPlayer);
        }
        actionController.improveTerraforming(currentPlayer);
        resourceController.obtainIncomeForImprovement(currentPlayer);
        return 0;
    }

    private Terrain getTerrain(int terrainID) {
        return game.getTerrain( terrainID );
    }



}
