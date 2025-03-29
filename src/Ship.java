class Ship {
    private int startRow;
    private int startCol;
    private int length;
    private boolean isHorizontal;
    private int hits;

    public Ship(int startRow, int startCol, int length, boolean isHorizontal) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.length = length;
        this.isHorizontal = isHorizontal;
        this.hits = 0;
    }

    boolean isHit(int row, int col){
        if (isHorizontal) {
            if (row == startRow && col >= startCol && col < startCol + length) {
                hits++;
                return true;
            }
        } else {
            if (col == startCol && row >= startRow && row < startRow + length) {
                hits++;
                return true;
            }
        }
        return false;
    }

    boolean isSunk(){
        return hits >= length;
    }

    public int getLength() {
        return length;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }
}
