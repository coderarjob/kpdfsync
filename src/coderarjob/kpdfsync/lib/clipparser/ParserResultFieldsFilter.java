/*
 * ParserResultFieldsFilter Class
 *
 * Holds word index of eah of the fields and well as the patterns which need to be matched.
 *
 * Dated: 16 July 2022
 * Author: arjobmukherjee@gmail.com
 */
package coderarjob.kpdfsync.lib.clipparser;

import java.util.Hashtable;

public class ParserResultFieldsFilter<T>
{
    private final int m_WordIndex;
    private Hashtable<String, T> m_Patterns;

    public ParserResultFieldsFilter (int wordIndex, Hashtable<String, T> patterns)
    {
      m_WordIndex = wordIndex;
      m_Patterns = patterns;
    }

    public int getWordIndex() { return m_WordIndex; }
    public Hashtable<String, T> getPatterns() { return m_Patterns; }
    public T getOrDefault(String key, T defaultValue)
    {
      return m_Patterns.getOrDefault(key, defaultValue);
    }
}
