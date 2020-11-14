package com.niks.unit.service;

import com.google.gson.Gson;
import com.niks.services.merge.MArrayMergeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.niks.model.SQSMessagePayload;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MArrayMergeServiceTest {

  @InjectMocks
  MArrayMergeService mArrayMergeService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMergeKSortedArrayWithEqualLength() {
    final Object[][] arrayToMerge = new Object[][]{{"allotment", "unconventionally"}, {"blanche", "coy"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);
    assertEquals(4, mergedArrayResult.length);
    assertEquals("allotment", mergedArrayResult[0]);
    assertEquals("blanche", mergedArrayResult[1]);
    assertEquals("coy", mergedArrayResult[2]);
    assertEquals("unconventionally", mergedArrayResult[3]);
  }

  @Test
  public void testMergeKSortedArrayWithDuplicateElements() {
    final String[][] arrayToMerge = new String[][]{{"aaaaa", "aaaaaa", "blanche", "coy", "fffffgg", "llll"},
        {"aaaaa", "aaaaaa", "fffffgg", "llll", "uuuughtfgy", "zzzzzzgthth"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("aaaaa", mergedArrayResult[0]);
    assertEquals("aaaaa", mergedArrayResult[1]);
    assertEquals("aaaaaa", mergedArrayResult[2]);
    assertEquals("aaaaaa", mergedArrayResult[3]);
    assertEquals("blanche", mergedArrayResult[4]);
    assertEquals("coy", mergedArrayResult[5]);
    assertEquals("fffffgg", mergedArrayResult[6]);
    assertEquals("fffffgg", mergedArrayResult[7]);
    assertEquals("llll", mergedArrayResult[8]);
    assertEquals("llll", mergedArrayResult[9]);
    assertEquals("uuuughtfgy", mergedArrayResult[10]);
    assertEquals("zzzzzzgthth", mergedArrayResult[11]);
  }

  @Test
  public void testMergeKSortedArrayWithDifferentLengthScenario1() {
    final String[][] arrayToMerge = new String[][]{{"aaaaa", "aaaaaa", "blanche", "coy"},
        {"aaaaa", "aaaaaa", "fffffgg", "llll", "uuuughtfgy", "zzzzzzgthth"}, {"apple", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("aaaaa", mergedArrayResult[0]);
    assertEquals("aaaaa", mergedArrayResult[1]);
    assertEquals("aaaaaa", mergedArrayResult[2]);
    assertEquals("aaaaaa", mergedArrayResult[3]);
    assertEquals("apple", mergedArrayResult[4]);
    assertEquals("blanche", mergedArrayResult[5]);
    assertEquals("coy", mergedArrayResult[6]);
    assertEquals("dog", mergedArrayResult[7]);
    assertEquals("fffffgg", mergedArrayResult[8]);
    assertEquals("llll", mergedArrayResult[9]);
    assertEquals("uuuughtfgy", mergedArrayResult[10]);
    assertEquals("zzzzzzgthth", mergedArrayResult[11]);
  }

  @Test
  public void testMergeKSortedArrayWithDifferentLengthScenario2() {
    final String[][] arrayToMerge = new String[][]{{"aaaaa"},
        {"aaaaa", "aaaaaa", "fffffgg", "llll", "uuuughtfgy", "zzzzzzgthth"}, {"aaaaaa","apple", "blanche", "coy", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("aaaaa", mergedArrayResult[0]);
    assertEquals("aaaaa", mergedArrayResult[1]);
    assertEquals("aaaaaa", mergedArrayResult[2]);
    assertEquals("aaaaaa", mergedArrayResult[3]);
    assertEquals("apple", mergedArrayResult[4]);
    assertEquals("blanche", mergedArrayResult[5]);
    assertEquals("coy", mergedArrayResult[6]);
    assertEquals("dog", mergedArrayResult[7]);
    assertEquals("fffffgg", mergedArrayResult[8]);
    assertEquals("llll", mergedArrayResult[9]);
    assertEquals("uuuughtfgy", mergedArrayResult[10]);
    assertEquals("zzzzzzgthth", mergedArrayResult[11]);
  }

  @Test
  public void testMergeKSortedArrayWithDifferentLengthScenario3() {
    final String[][] arrayToMerge = new String[][]{{"aaaaa", "fffffgg", "llll","YYGHYGTHTFGGH"},
        { "uuuughtfgy", "zzzzzzgthth"}, {"aaaaaa","apple", "blanche", "coy", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("aaaaa", mergedArrayResult[0]);
    assertEquals("aaaaaa", mergedArrayResult[1]);
    assertEquals("apple", mergedArrayResult[2]);
    assertEquals("blanche", mergedArrayResult[3]);
    assertEquals("coy", mergedArrayResult[4]);
    assertEquals("dog", mergedArrayResult[5]);
    assertEquals("fffffgg", mergedArrayResult[6]);
    assertEquals("llll", mergedArrayResult[7]);
    assertEquals("uuuughtfgy", mergedArrayResult[8]);
    assertEquals("YYGHYGTHTFGGH", mergedArrayResult[9]);
    assertEquals("zzzzzzgthth", mergedArrayResult[10]);
  }

  @Test
  public void testMergeKSortedArrayWithDifferentLengthScenario4() {
    final String[][] arrayToMerge = new String[][]{{"aaaaa", "f_ffffgg", "llll","YYGHYGTHTFGGH"},
        { "uuuughtfgy", "zzzzzzgthth"}, {"aaaaaa","apple", "blanche", "coy", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("aaaaa", mergedArrayResult[0]);
    assertEquals("aaaaaa", mergedArrayResult[1]);
    assertEquals("apple", mergedArrayResult[2]);
    assertEquals("blanche", mergedArrayResult[3]);
    assertEquals("coy", mergedArrayResult[4]);
    assertEquals("dog", mergedArrayResult[5]);
    assertEquals("f_ffffgg", mergedArrayResult[6]);
    assertEquals("llll", mergedArrayResult[7]);
    assertEquals("uuuughtfgy", mergedArrayResult[8]);
    assertEquals("YYGHYGTHTFGGH", mergedArrayResult[9]);
    assertEquals("zzzzzzgthth", mergedArrayResult[10]);
  }

  @Test
  public void testMergeKSortedArrayWithDifferentLengthScenario5() {
    final String[][] arrayToMerge = new String[][]{{"1", "2", "3","4"},
        { "uuuughtfgy", "zzzzzzgthth"}, {"aaaaaa","apple", "blanche", "coy", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("1", mergedArrayResult[0]);
    assertEquals("2", mergedArrayResult[1]);
    assertEquals("3", mergedArrayResult[2]);
    assertEquals("4", mergedArrayResult[3]);
    assertEquals("aaaaaa", mergedArrayResult[4]);
    assertEquals("apple", mergedArrayResult[5]);
    assertEquals("blanche", mergedArrayResult[6]);
    assertEquals("coy", mergedArrayResult[7]);
    assertEquals("dog", mergedArrayResult[8]);
    assertEquals("uuuughtfgy", mergedArrayResult[9]);
    assertEquals("zzzzzzgthth", mergedArrayResult[10]);
  }

  @Test
  public void testMergeKSortedArrayWithCapitalAndSmallCaseWord() {
    final String[][] arrayToMerge = new String[][]{{"Aaa", "aaaaaa", "blanche", "coy"},
        {"aaa", "aaaaaa", "fffffgg", "KKKK", "uuuughtfgy", "zzzzzzgthth"}, {"CAT", "dog"}};
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());
    String[] mergedArrayResult = mArrayMergeService.mergeMSortedArray(arrayToMerge, numberOfArraysToMerge,sqsMessageBody);

    assertEquals("Aaa", mergedArrayResult[0]);
    assertEquals("aaa", mergedArrayResult[1]);
    assertEquals("aaaaaa", mergedArrayResult[2]);
    assertEquals("aaaaaa", mergedArrayResult[3]);
    assertEquals("blanche", mergedArrayResult[4]);
    assertEquals("CAT", mergedArrayResult[5]);
    assertEquals("coy", mergedArrayResult[6]);
    assertEquals("dog", mergedArrayResult[7]);
    assertEquals("fffffgg", mergedArrayResult[8]);
    assertEquals("KKKK", mergedArrayResult[9]);
    assertEquals("uuuughtfgy", mergedArrayResult[10]);
    assertEquals("zzzzzzgthth", mergedArrayResult[11]);
  }

  private SQSMessagePayload getSQSMessagePayload() {
    SQSMessagePayload sqsMessagePayload = new SQSMessagePayload();
    sqsMessagePayload.setBucketName("niks");
    sqsMessagePayload.setFolderName("input");
    sqsMessagePayload.setFolderPath("119D3831852F51/input/test.dat");
    sqsMessagePayload.setTenantId("119D3831852F51");
    return sqsMessagePayload;
  }
}
