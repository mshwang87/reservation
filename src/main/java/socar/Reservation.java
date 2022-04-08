package socar;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;


@Entity
@Table(name="Reservation_table")
public class Reservation  {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long rsvId;
    private Long carId;
    private String status; // VALUE = reqReserve, reserved, reqCancel, cancelled
    private Long payId; // 결제 ID : 결재 완료시 Update, 결제 취소하는 경우 사용


    @PostPersist
    public void onPostPersist(){

        // 예약 요청이 들어왔을 경우 사용가능한지 확인
        // mappings goes here
        boolean result = ReservationApplication.applicationContext.getBean(socar.external.CarService.class)
            .chkAndReqReserve(this.getCarId());
        System.out.println("사용가능 여부 : " + result);

        if(result) { 

            // 예약 가능한 상태인 경우(Available)
            // PAYMENT 결제모듈 호출 (POST방식) - SYNC 호출
            socar.external.Payment payment = new socar.external.Payment();
            payment.setRsvId(this.getRsvId());
            payment.setCarId(this.getCarId());
            payment.setStatus("paid");
            ReservationApplication.applicationContext.getBean(socar.external.PaymentService.class)
                .approvePayment(payment);

            // 이벤트시작 --> ReservationCreated
            ReservationCreated reservationCreated = new ReservationCreated();
            BeanUtils.copyProperties(this, reservationCreated);
            reservationCreated.publishAfterCommit();
        }
    }

    @PostUpdate
    public void onPostUpdate(){

        // 예약 취소 요청일 경우 
        if(this.getStatus().equals("reqCancel")) {
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();
        }

        // 예약 확정일 경우 
        if(this.getStatus().equals("reserved")) {
            ReservationConfirmed reservationConfirmed = new ReservationConfirmed();
            BeanUtils.copyProperties(this, reservationConfirmed);
            reservationConfirmed.publishAfterCommit();
        }

        // 예약 취소일 경우 
        if(this.getStatus().equals("cancelled")) {
            ReservationCancelled reservationCancelled = new ReservationCancelled();
            BeanUtils.copyProperties(this, reservationCancelled);
            reservationCancelled.publishAfterCommit();
        }

    }
    @PrePersist
    public void onPrePersist(){
    }
    @PreUpdate
    public void onPreUpdate(){
    }

    public Long getRsvId() {
        return rsvId;
    }

    public void setRsvId(Long rsvId) {
        this.rsvId = rsvId;
    }
    
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
    



}
