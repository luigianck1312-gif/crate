package it.cratePlugin.models;

public class PlayerQuest {

    private final String questId;
    private int progress;
    private boolean completed;
    private boolean rewardClaimed;

    public PlayerQuest(String questId) {
        this.questId = questId;
        this.progress = 0;
        this.completed = false;
        this.rewardClaimed = false;
    }

    public PlayerQuest(String questId, int progress, boolean completed, boolean rewardClaimed) {
        this.questId = questId;
        this.progress = progress;
        this.completed = completed;
        this.rewardClaimed = rewardClaimed;
    }

    public String getQuestId()         { return questId; }
    public int getProgress()           { return progress; }
    public boolean isCompleted()       { return completed; }
    public boolean isRewardClaimed()   { return rewardClaimed; }

    public void addProgress(int amount) {
        this.progress += amount;
    }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setRewardClaimed(boolean rewardClaimed) { this.rewardClaimed = rewardClaimed; }
    public void setProgress(int progress) { this.progress = progress; }
}
