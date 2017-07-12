package com.ichano.cbp;

import java.util.HashMap;
import java.util.Map;

public class CbpBunch {
	private class CbpParamKey {
		private int m_uiType;
		private int m_uiTag;

		public CbpParamKey(int uiType, int uiTag) {
			this.m_uiType = uiType;
			this.m_uiTag = uiTag;
		}

		public int getType() {
			return this.m_uiType;
		}

		public int getTag() {
			return this.m_uiTag;
		}

		public boolean equals(Object obj) {
			CbpParamKey cmpParam = (CbpParamKey) obj;
			return (cmpParam.m_uiType == this.m_uiType) && (cmpParam.m_uiTag == cmpParam.m_uiTag);
		}

		public int hashCode() {
			return this.m_uiType * 100000 + this.m_uiTag;
		}

		public String toString() {
			return "CbpKey@" + this.m_uiType + "_" + this.m_uiTag;
		}
	}

	private HashMap<CbpParamKey, Object> mParamHash = new HashMap();

	public int addUI(int uiTag, int uiVal) {
		this.mParamHash.put(new CbpParamKey(1, uiTag), Integer.valueOf(uiVal));
		return 0;
	}

	public int addString(int uiTag, String strVal) {
		this.mParamHash.put(new CbpParamKey(2, uiTag), strVal);
		return 0;
	}

	public int addHandle(int uiTag, long hVal) {
		this.mParamHash.put(new CbpParamKey(3, uiTag), Long.valueOf(hVal));
		return 0;
	}

	public int addXXLSIZE(int uiTag, long hVal) {
		this.mParamHash.put(new CbpParamKey(5, uiTag), Long.valueOf(hVal));
		return 0;
	}

	public int addBool(int uiTag, boolean bVal) {
		this.mParamHash.put(new CbpParamKey(0, uiTag), Boolean.valueOf(bVal));
		return 0;
	}

	public int getUI(int uiTag, int uiDefault) {
		Integer uiVal = (Integer) this.mParamHash.get(new CbpParamKey(1, uiTag));
		return uiVal == null ? uiDefault : uiVal.intValue();
	}

	public String getString(int uiTag) {
		return (String) this.mParamHash.get(new CbpParamKey(2, uiTag));
	}

	public long getHandle(int uiTag) {
		Long hVal = (Long) this.mParamHash.get(new CbpParamKey(3, uiTag));
		return hVal == null ? 0L : hVal.longValue();
	}

	public long getXXLSIZE(int uiTag, long uiDefault) {
		Long uiVal = (Long) this.mParamHash.get(new CbpParamKey(5, uiTag));
		return uiVal == null ? uiDefault : uiVal.longValue();
	}

	public boolean getBool(int uiTag, boolean bDefault) {
		Boolean bVal = (Boolean) this.mParamHash.get(new CbpParamKey(0, uiTag));
		return bVal == null ? bDefault : bVal.booleanValue();
	}

	public boolean containsTag(int uiTag) {
		for (Map.Entry<CbpParamKey, Object> intEntry : this.mParamHash.entrySet()) {
			CbpParamKey param = (CbpParamKey) intEntry.getKey();
			if (uiTag == param.getTag()) {
				return true;
			}
		}
		return false;
	}

	public int loopGetter(GetRunner runner) {
		for (Map.Entry<CbpParamKey, Object> intEntry : this.mParamHash.entrySet()) {
			CbpParamKey param = (CbpParamKey) intEntry.getKey();
			switch (param.getType()) {
			case 0:
				runner.doBool(param.getTag(), ((Boolean) intEntry.getValue()).booleanValue());
				break;
			case 1:
				runner.doUI(param.getTag(), ((Integer) intEntry.getValue()).intValue());
				break;
			case 2:
				runner.doStr(param.getTag(), (String) intEntry.getValue());
				break;
			case 3:
				runner.doHandle(param.getTag(), ((Long) intEntry.getValue()).longValue());
				break;
			case 5:
				runner.doXXLSize(param.getTag(), ((Long) intEntry.getValue()).longValue());
			}
		}
		return 0;
	}

	public static abstract interface GetRunner {
		public abstract int doBool(int paramInt, boolean paramBoolean);

		public abstract int doUI(int paramInt1, int paramInt2);

		public abstract int doStr(int paramInt, String paramString);

		public abstract int doXXLSize(int paramInt, long paramLong);

		public abstract int doHandle(int paramInt, long paramLong);
	}
}
