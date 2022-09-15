## Local 
### Syn
```shell
curl -H "Content-Type:application/json" -XPOST 'http://localhost:18087/external/master/remind_task/syn?disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=true' -d '{"t":1663216704716,"sign":"$1$KjOJBazn$LNGQDqbCRgZXoeCPxpVmz/","uid":"188840383@qq.com","taskList":[{"id":"188840383_01","colId":"T_01","compId":"T_01","href":"/T_01/T_01","title":"Test","content":null,"expectTriggerTime":1663217204718,"extra":null}]}' 
```
### List
```shell
curl  'http://localhost:18087/external/master/remind_task/list?uid=188840383@qq.com&t=1&sign=1&disableMasterFpCheck=true&disableTimestampCheck=true&disableSignCheck=true'
```