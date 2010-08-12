<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

	<xsl:template match="sysex.sysexSound">
		<genericSound>
			<xsl:apply-templates select="@*|output"/>
		</genericSound>
	</xsl:template>

	<xsl:template match="sysex.sysexConsole">
		<console>
			<xsl:apply-templates select="@*|skin|screen|input|output|style|zoom"/>
		</console>
	</xsl:template>

	<xsl:template match="stop|coupler">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<from>0</from>
			<to>127</to>
		</xsl:copy>
	</xsl:template>

  	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>		
	
 </xsl:stylesheet>