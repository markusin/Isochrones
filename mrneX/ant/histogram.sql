SELECT COUNT(*) FROM (
SELECT MIN(S.DEPARTURE_TIME) FROM @SCHEDULE_TABLE@ S,
(SELECT DISTINCT SERVICE_ID FROM @DAYMARKER_TABLE@ 
WHERE 
@WEEK_DAY@ = TRUE AND 
START_DATE <= TO_TIMESTAMP('@ARRIVAL_DAY@','@ARRIVAL_DAY_PATTERN@') AND 
TO_TIMESTAMP('@ARRIVAL_DAY@','@ARRIVAL_DAY_PATTERN@') <= END_DATE
) C
WHERE 
S.SERVICE_ID = C.SERVICE_ID AND
S.DEPARTURE_TIME>=TO_TIMESTAMP('@TIMESTAMP@','@TIMESTAMP_PATTERN@') + INTERVAL '@FROM_INC@' HOUR
AND 
S.ARRIVAL_TIME<TO_TIMESTAMP('@TIMESTAMP@','@TIMESTAMP_PATTERN@')    + INTERVAL '@TO_INC@' HOUR
GROUP BY TRIP_ID) D