def findMedianSortedArrays(nums1, nums2):
    if len(nums1) > len(nums2):
        nums1, nums2 = nums2, nums1
    m, n = len(nums1), len(nums2)
    left, right = 0, m
    while left <= right:
        partition1 = (left + right) // 2
        partition2 = (m + n + 1) // 2 - partition1
        max1 = float('-inf') if partition1 == 0 else nums1[partition1 - 1]
        min1 = float('inf') if partition1 == m else nums1[partition1]
        max2 = float('-inf') if partition2 == 0 else nums2[partition2 - 1]
        min2 = float('inf') if partition2 == n else nums2[partition2]
        if max1 <= min2 and max2 <= min1:
            if (m + n) % 2 == 0:
                return (max(max1, max2) + min(min1, min2)) / 2
            else:
                return max(max1, max2)
        elif max1 > min2:
            right = partition1 - 1
        else:
            left = partition1 + 1
