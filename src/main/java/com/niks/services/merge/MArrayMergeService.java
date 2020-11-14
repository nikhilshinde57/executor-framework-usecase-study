package com.niks.services.merge;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.niks.constants.HelperConstants;
import com.niks.utils.MinHeap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MArrayMergeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MArrayMergeService.class);
  private static final SimpleDateFormat sdf = new SimpleDateFormat(HelperConstants.DATE_FORMAT);

  public String[] mergeMSortedArray(final Object[][] arraysToMerge, final int numberOfArraysToMerge,
      final String messageBody) {
    LOGGER.info(String.format("Merging of %s arrays has been started at %s for message:%s ", arraysToMerge.length,
        sdf.format(new Date()), messageBody));

    String[] mergedArrays = MinHeap.mergeKSortedArrays(arraysToMerge, numberOfArraysToMerge);

    LOGGER.info(String.format("Merging of %s arrays has been completed at %s for message:%s ", arraysToMerge.length,
        sdf.format(new Date()), messageBody));
    return mergedArrays;
  }
}
