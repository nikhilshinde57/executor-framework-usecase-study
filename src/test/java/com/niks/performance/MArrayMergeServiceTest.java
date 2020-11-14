package com.niks.performance;

import static org.junit.Assert.assertEquals;

import com.niks.services.merge.MArrayMergeService;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import com.niks.constants.HelperConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.niks.model.SQSMessagePayload;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MArrayMergeServiceTest {

  @InjectMocks
  MArrayMergeService mArrayMergeService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MArrayMergeServiceTest.class);
  private static final SimpleDateFormat sdf = new SimpleDateFormat(HelperConstants.DATE_FORMAT);

  @Test
  public void testMergeKSortedArrayWithEqualLength() throws IOException {

    final String[][] arrayToMerge = readFiles();
    List<String> sortedListUsingCollectionMethod = readFilesAndGetList();
    int numberOfArraysToMerge = arrayToMerge.length;
    String sqsMessageBody = new Gson().toJson(getSQSMessagePayload());

    String[] mergedArrayResult = mArrayMergeService
        .mergeMSortedArray(arrayToMerge, numberOfArraysToMerge, sqsMessageBody);

    LOGGER.info(String
        .format("Sorting of %s words has been started at %s for message:%s ", sortedListUsingCollectionMethod.size(),
            sdf.format(new Date()), sqsMessageBody));
    Collections.sort(sortedListUsingCollectionMethod);

    LOGGER.info(String
        .format("Sorting of %s words has been completed at %s for message:%s ", sortedListUsingCollectionMethod.size(),
            sdf.format(new Date()), sqsMessageBody));

    assertEquals(sortedListUsingCollectionMethod.size(), mergedArrayResult.length);
    for (int i = 0; i < mergedArrayResult.length; i++) {
      assertEquals(sortedListUsingCollectionMethod.get(i), mergedArrayResult[i]);
    }
  }

  private SQSMessagePayload getSQSMessagePayload() {
    SQSMessagePayload sqsMessagePayload = new SQSMessagePayload();
    sqsMessagePayload.setBucketName("niks");
    sqsMessagePayload.setFolderName("input");
    sqsMessagePayload.setFolderPath("119D3831852F51/input/test.dat");
    sqsMessagePayload.setTenantId("119D3831852F51");
    return sqsMessagePayload;
  }

  //Written for preparing test data
  private String[][] readFiles() throws IOException {

    String path = "src/test/resources/functional/input";
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    String[][] arrayToMerge = new String[listOfFiles.length][];
    int cnt = 0;

    for (File file : listOfFiles) {
      if (file.isFile()) {
        List<String> stringList = new ArrayList<>();
        Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()));
        stream.forEach(
            line -> {
              stringList.add(line.trim());
            });
        arrayToMerge[cnt++] = stringList.toArray(new String[stringList.size()]);
      }
    }
    return arrayToMerge;
  }

  private List<String> readFilesAndGetList() throws IOException {

    String path = "src/test/resources/functional/input";
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    String[][] arrayToMerge = new String[listOfFiles.length][];
    List<String> stringList = new ArrayList<>();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()));
        stream.forEach(
            line -> {
              //stringList.addAll(Arrays.asList(line.split("\n")));
              stringList.add(line.trim());
            });
      }
    }
    return stringList;
  }

  //Written for preparing test data
  private void prepareTestData() throws IOException {
    List<String> dataToWrite = readFilesAndGetList();
    Collections.sort(dataToWrite);
    String temp = dataToWrite.toString();
    temp = temp.replace("[", "").
        replace("]", "").
        replace(" ", HelperConstants.EMPTY_STRING).
        replace(",", "\n");

    int cnt = 1;
    while (cnt < 11) {
      String file = "src/test/resources/functional/output/input_with_50k_words_" + cnt + ".dat";
      File t = new File(file);
      try (RandomAccessFile writer = new RandomAccessFile(t.getAbsolutePath(), "rw");
          FileChannel channel = writer.getChannel()) {
        ByteBuffer buff = ByteBuffer.wrap(temp.getBytes(StandardCharsets.UTF_8));
        channel.write(buff);
        cnt++;
        System.out.println("Writing to output file completed: " + new Date());
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Err");
      }
    }
  }
}
