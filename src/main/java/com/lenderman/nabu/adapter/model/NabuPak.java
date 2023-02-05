package com.lenderman.nabu.adapter.model;

import java.util.List;

public class NabuPak
{
    /**
     * List of all the segments in this pak
     */
    private List<NabuSegment> pakSegments;

    /**
     * Name of the pak
     */
    private String pakName;

    /**
     * Constructor
     */
    public NabuPak(List<NabuSegment> pakSegments, String pakName)
    {
        this.pakSegments = pakSegments;
        this.pakName = pakName;
    }

    /**
     * @return List<NabuSegment>
     */
    public List<NabuSegment> getPakSegments()
    {
        return pakSegments;
    }

    /**
     * @param List<NabuSegment>
     */
    public void setPakSegments(List<NabuSegment> pakSegments)
    {
        this.pakSegments = pakSegments;
    }

    /**
     * @return String
     */
    public String getPakName()
    {
        return pakName;
    }

    /**
     * @param pakName
     */
    public void setPakName(String pakName)
    {
        this.pakName = pakName;
    }
}
