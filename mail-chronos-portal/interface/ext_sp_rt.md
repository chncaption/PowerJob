## Local 
### Syn
```shell
curl -H "Content-Type:application/json" -XPOST 'http://localhost:18087/external/master/remind_task/syn?disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=true' -d '{"t":1663216704716,"sign":"$1$KjOJBazn$LNGQDqbCRgZXoeCPxpVmz/","uid":"188840383@qq.com","taskList":[{"id":"188840383_01","colId":"T_01","compId":"T_01","href":"/T_01/T_01","title":"Test","content":null,"expectTriggerTime":1663217204718,"extra":null}]}' 
```
### List
```shell
curl  'http://localhost:18087/external/master/remind_task/list?uid=188840383@qq.com&t=1&sign=1&disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=true'
```

## Test
### Syn
```shell
curl -H "Content-Type:application/json" -XPOST 'https://dashi.163.com/fgw/chronos-portal/external/master/remind_task/syn?disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=false' -d '{"t":1663216704716,"sign":"de5511c1577a38a6c1367077cf992f39","uid":"188840383@qq.com","taskList":[{"id":"188840383_01","colId":"CALENDAR-DEFAULT-TASKS","compId":"44a354ea-2a66-4dbb-9717-02fcc9877b2d","href":"/caldav/dav/6e1c551a5484c94aba99acef62d56af6/calendar/CALENDAR-DEFAULT-TASKS/44a354ea-2a66-4dbb-9717-02fcc9877b2d.ics","title":"Test","content":"hello ~","expectTriggerTime":1663217204718,"extra":null}]}' 
```
### List
```shell
curl 'https://dashi.163.com/fgw/chronos-portal/external/master/remind_task/list?disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=true&t=1663216704716&sign=s&uid=188840383@qq.com' 
```