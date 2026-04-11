package com.skillsync.userservice.dto.response;

public class UserStatsDTO {

    private long total;
    private long learners;
    private long mentors;
    private long admins;

    public UserStatsDTO() {}

    public UserStatsDTO(long total, long learners, long mentors, long admins) {
        this.total    = total;
        this.learners = learners;
        this.mentors  = mentors;
        this.admins   = admins;
    }

    public long getTotal()    { return total; }
    public long getLearners() { return learners; }
    public long getMentors()  { return mentors; }
    public long getAdmins()   { return admins; }

    public void setTotal(long total)       { this.total = total; }
    public void setLearners(long learners) { this.learners = learners; }
    public void setMentors(long mentors)   { this.mentors = mentors; }
    public void setAdmins(long admins)     { this.admins = admins; }
}
