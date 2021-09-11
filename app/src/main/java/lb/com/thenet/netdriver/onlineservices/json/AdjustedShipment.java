package lb.com.thenet.netdriver.onlineservices.json;

public class AdjustedShipment {
    public AdjustedShipment(){

    }
    public AdjustedShipment(double length, double width, double heigh, double chargeable, double volumetric, double volume, Integer nop){
        Dimension dimension = new Dimension();
        Weight weight = new Weight();
        dimension.length = length;
        dimension.height = heigh;
        dimension.width = width;
        weight.chargeable = chargeable;
        weight.volume = volume;
        weight.volumetric = volumetric;
        weight.nop = nop;
        this.weight = weight;
        this.dimension = dimension;
    }
    public String reference;
    public Dimension dimension;
    public Weight weight;
}
