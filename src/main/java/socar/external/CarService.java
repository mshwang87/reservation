package socar.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="car", url="http://user06-car:8080")
public interface CarService {
    @RequestMapping(method= RequestMethod.GET, path="/cars")
    public void chkAndReqReserve(@RequestBody Car car);

}

