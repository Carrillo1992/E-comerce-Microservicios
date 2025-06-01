package com.dcarrillo.ecomerce.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "item_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idProduct;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @NotNull
    @Column(nullable = false)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @PrePersist
    @PreUpdate
    public void calcularSubTotal(){
        if (this.unitPrice !=null && this.quantity !=null && this.quantity > 0){
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
