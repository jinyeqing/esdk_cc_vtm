package com.huawei.vtm.call.data;

import com.huawei.vtm.StringUtils;

public class VoiceQuality
{

	public enum VoiceQualityLevel
	{
		POOL, NORMAL_1, NORMAL_2, NORMAL_3, EXCELLENT
	}

	/**
	 * 语音质量 1:差 3:一般 5:好
	 */
	private VoiceQualityLevel level = VoiceQualityLevel.POOL;

	public VoiceQualityLevel getLevel()
	{
		return level;
	}

	public void setLevel(VoiceQualityLevel level)
	{
		this.level = level;
	}

	public VoiceQualityLevel convertFrom(String param)
	{
		int ret = StringUtils.stringToInt(param);

		switch (ret)
		{
		case 1:
			level = VoiceQualityLevel.POOL;
			break;
		case 2:
			level = VoiceQualityLevel.NORMAL_1;
			break;
		case 3:
			level = VoiceQualityLevel.NORMAL_2;
			break;
		case 4:
			level = VoiceQualityLevel.NORMAL_3;
			break;
		case 5:
			level = VoiceQualityLevel.EXCELLENT;
			break;
		default:
			level = VoiceQualityLevel.EXCELLENT;
		}
		return level;
	}

	@Override
	public String toString()
	{
		return "level = " + level;
	}

}
