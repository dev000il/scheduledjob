package cn.rfidcn.scheduledjob.model;

public	class IdRange{
		int start;
		int end;
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		public IdRange(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
		
	}

